<?php

namespace App\Form;

use App\Entity\Candidat;
use App\Entity\Offre;
use App\Entity\Recrutement;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class RecrutementType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('offre', EntityType::class, ['class' => Offre::class, 'choice_label' => 'nomOffre'])
            ->add('candidat', EntityType::class, ['class' => Candidat::class, 'choice_label' => 'nom']);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Recrutement::class]);
    }
}
